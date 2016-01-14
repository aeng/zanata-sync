package org.zanata.helper.repository;

import java.io.File;
import java.util.List;
import java.util.function.BiFunction;
import javax.decorator.Decorator;
import javax.decorator.Delegate;
import javax.inject.Inject;

import org.apache.commons.io.Charsets;
import org.zanata.helper.component.AppConfiguration;
import org.zanata.helper.model.SyncWorkConfig;
import org.zanata.helper.util.EncryptionUtil;
import com.google.common.base.Strings;

/**
 * @author Patrick Huang <a href="mailto:pahuang@redhat.com">pahuang@redhat.com</a>
 */
@Decorator
public class SyncWorkConfigSerializerEncryptionDecorator
        implements SyncWorkConfigSerializer {
    @Inject
    @Delegate
    private SyncWorkConfigSerializerImpl delegate;

    @Inject
    private AppConfiguration appConfiguration;

    @Override
    public SyncWorkConfig fromYaml(File file) {
        List<String> fieldsNeedEncryption =
                appConfiguration.getFieldsNeedEncryption();
        SyncWorkConfig configOnDisk = delegate.fromYaml(file);
        String encryptionKey = configOnDisk.getEncryptionKey();

        if (fieldsNeedEncryption.isEmpty() ||
                Strings.isNullOrEmpty(encryptionKey)) {
            return configOnDisk;
        }

        // start the decryption
        EncryptionUtil encryptionUtil =
                new EncryptionUtil(encryptionKey.getBytes(Charsets.UTF_8));

        BiFunction<String, String, String> decryptFunc =
                (key, value) -> encryptionUtil.decrypt(value);
        for (String field : fieldsNeedEncryption) {
            configOnDisk.getSrcRepoPluginConfig().computeIfPresent(field,
                    decryptFunc);
            configOnDisk.getTransServerConfig().computeIfPresent(field,
                    decryptFunc);
        }

        return configOnDisk;
    }

    @Override
    public String toYaml(SyncWorkConfig syncWorkConfig) {
        List<String> fieldsNeedEncryption =
                appConfiguration.getFieldsNeedEncryption();
        String encryptionKey = syncWorkConfig.getEncryptionKey();

        if (fieldsNeedEncryption.isEmpty() ||
                Strings.isNullOrEmpty(encryptionKey)) {
            return delegate.toYaml(syncWorkConfig);
        }

        // start the encryption
        EncryptionUtil encryptionUtil =
                new EncryptionUtil(encryptionKey.getBytes(Charsets.UTF_8));

        BiFunction<String, String, String> encryptFunc =
                (key, value) -> encryptionUtil.encrypt(value);
        for (String field : fieldsNeedEncryption) {
            syncWorkConfig.getSrcRepoPluginConfig().computeIfPresent(field,
                    encryptFunc);
            syncWorkConfig.getTransServerConfig().computeIfPresent(field,
                    encryptFunc);
        }

        return delegate.toYaml(syncWorkConfig);
    }

}