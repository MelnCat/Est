package dev.melncat.est.classloader;

import java.net.URLClassLoader;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;

public class EstClassLoader extends URLClassLoader {
	private final ClassLoader nova;
	private final URLClassLoader libLoader;

	private List<String> a = ((Supplier<List<String>>) () -> {
		List<String> list = new ArrayList<>();
		list.add("hello");
		return list;
	}).get();

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