package jp.webpay.android.ui;

import org.junit.runners.model.InitializationError;
import org.robolectric.AndroidManifest;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.res.DrawableNode;
import org.robolectric.res.OverlayResourceLoader;
import org.robolectric.res.PackageResourceLoader;
import org.robolectric.res.ResName;
import org.robolectric.res.ResourceLoader;
import org.robolectric.res.ResourcePath;
import org.robolectric.res.RoutingResourceLoader;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Custom robolectric test runner.
 *
 * This runner replaces {@code ResourceLoader} for target package's name space,
 * in order to resolve resources from other libraries (such as appcompat).
 * They are neither shipped with Robolectric, nor linked in SUT compiled resources.
 *
 * One way is to copy the resource into our source, but it is bad from maintenance and
 * license points of view. Because the resource contents are not related to test results
 * in case of drawable, we prefer to replace them with some indifferent image.
 *
 * {@see https://github.com/robolectric/robolectric/issues/1375}
 */
public class RobolectricTestRunnerWithDummyResources extends RobolectricTestRunner {
    /**
     * Creates a runner to run {@code testClass}. Looks in your working directory for your AndroidManifest.xml file
     * and res directory by default. Use the {@link org.robolectric.annotation.Config} annotation to configure.
     *
     * @param testClass the test class to be run
     * @throws org.junit.runners.model.InitializationError if junit says so
     */
    public RobolectricTestRunnerWithDummyResources(Class<?> testClass) throws InitializationError {
        super(testClass);
    }

    // use OverlayResourceLoaderWithDummyResources to replace resources from AppCompat
    @Override
    protected ResourceLoader createAppResourceLoader(ResourceLoader systemResourceLoader, AndroidManifest appManifest) {
        List<PackageResourceLoader> appAndLibraryResourceLoaders = new ArrayList<PackageResourceLoader>();
        for (ResourcePath resourcePath : appManifest.getIncludedResourcePaths()) {
            appAndLibraryResourceLoaders.add(createResourceLoader(resourcePath));
        }
        OverlayResourceLoader overlayResourceLoader =
                new OverlayResourceLoaderWithDummyResources(
                        appManifest.getPackageName(), appAndLibraryResourceLoaders, systemResourceLoader);

        Map<String, ResourceLoader> resourceLoaders = new HashMap<String, ResourceLoader>();
        resourceLoaders.put("android", systemResourceLoader);
        resourceLoaders.put(appManifest.getPackageName(), overlayResourceLoader);
        return new RoutingResourceLoader(resourceLoaders);
    }

    private static class OverlayResourceLoaderWithDummyResources extends OverlayResourceLoader {
        private static final ResName DUMMY_RES_NAME = new ResName("android", "drawable", "unknown_image");
        private final ResourceLoader systemResourceLoader;

        public OverlayResourceLoaderWithDummyResources(String packageName,
                                                       List<PackageResourceLoader> subResourceLoaders,
                                                       ResourceLoader systemResourceLoader) {
            super(packageName, subResourceLoaders);
            this.systemResourceLoader = systemResourceLoader;
        }

        @Override
        public DrawableNode getDrawableNode(ResName resName, String qualifiers) {
            DrawableNode node = super.getDrawableNode(resName, qualifiers);
            if (node != null)
                return node;
            return systemResourceLoader.getDrawableNode(DUMMY_RES_NAME, qualifiers);
        }
    }
}
