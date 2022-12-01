package dev.melncat.est.classloader;

import java.net.URLClassLoader;

public class EstClassLoader extends URLClassLoader {
	private final ClassLoader nova;
	private final URLClassLoader libLoader;

	public EstClassLoader(ClassLoader nova, URLClassLoader libLoader) {
		super(libLoader.getURLs(), libLoader.getParent());
		this.nova = nova;
		this.libLoader = libLoader;

	}

	@Override
	public Class<?> loadClass(String name) throws ClassNotFoundException {
		try {
			return libLoader.loadClass(name);
		} catch (ClassNotFoundException ignored) {}
		if (name.startsWith("xyz.xenondevs") || name.startsWith("de.studiocode")) return nova.loadClass(name);
		throw new ClassNotFoundException(name);
	}

	@Override
	public Class<?> loadClass(String name, boolean resolve) throws ClassNotFoundException {
		return loadClass(name);
	}
}