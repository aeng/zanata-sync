package org.zanata.helper.service.impl;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.zanata.helper.common.plugin.SourceRepoExecutor;
import org.zanata.helper.common.plugin.TranslationServerExecutor;
import org.zanata.helper.exception.UnableLoadPluginException;
import org.zanata.helper.service.PluginsService;

import javax.annotation.PostConstruct;
import java.util.HashMap;
import java.util.Map;

/**
 * @author Alex Eng <a href="aeng@redhat.com">aeng@redhat.com</a>
 */
@Service
@Slf4j
public final class PluginsServiceImpl implements PluginsService {

    private final static Map<String, Class<? extends SourceRepoExecutor>>
        sourceRepoPluginMap =
        new HashMap<String, Class<? extends SourceRepoExecutor>>();

    private final static Map<String, Class<? extends TranslationServerExecutor>>
        transServerPluginMap =
        new HashMap<String, Class<? extends TranslationServerExecutor>>();

    /**
     * Initiate all plugins available
     */
    @PostConstruct
    public void initialise() {
        /**
         * TODO: scan classpath for plugins class, remove plugins dependency from server module
         * For now, load all known plugins
         */

        sourceRepoPluginMap
            .put(org.zanata.helper.plugin.git.Plugin.class.getName(),
                org.zanata.helper.plugin.git.Plugin.class);

        transServerPluginMap
            .put(org.zanata.helper.plugin.zanata.Plugin.class.getName(),
                org.zanata.helper.plugin.zanata.Plugin.class);
    }

    @Override
    public SourceRepoExecutor getNewSourceRepoPlugin(String className,
        Map<String, String> fields) throws UnableLoadPluginException {
        Class<? extends SourceRepoExecutor>
            executor = sourceRepoPluginMap.get(className);
        try {
            return executor.getDeclaredConstructor(Map.class)
                .newInstance(fields);
        } catch (Exception e) {
            throw new UnableLoadPluginException(className);
        }
    }

    @Override
    public TranslationServerExecutor getNewTransServerPlugin(
        String className,
        Map<String, String> fields) throws UnableLoadPluginException {
        Class<? extends TranslationServerExecutor>
            executor = transServerPluginMap.get(className);
        try {
            return executor.getDeclaredConstructor(Map.class)
                .newInstance(fields);
        } catch (Exception e) {
            throw new UnableLoadPluginException(className);
        }
    }

//    public static void main(String args[]) {
//        System.out.println(org.zanata.helper.plugin.git.Plugin.class.getName());
////        sourceRepoPluginMap.put("gitPlugin",
////            org.zanata.helper.plugin.git.Plugin.class);
////        SourceRepoExecutor executor = getNewSourceRepoPlugin("gitPlugin", null);
//    }
}
