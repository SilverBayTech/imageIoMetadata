/*
 * Copyright (c) 2014 Kevin Hunter
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.silverbaytech.blog.imageIoMetadata;

import java.util.Iterator;

import javax.imageio.ImageIO;
import javax.imageio.spi.IIORegistry;
import javax.imageio.spi.ImageReaderSpi;
import javax.imageio.spi.ImageReaderWriterSpi;
import javax.imageio.spi.ImageWriterSpi;

/**
 * A simple program to dump information about ImageIO plugins defined in the
 * system.
 * 
 * @author Kevin Hunter
 * 
 */
public class DumpImageIoPlugins
{
	private static void indent(int indent)
	{
		for (int i = 0; i < indent; i++)
		{
			System.out.print("    ");
		}
	}

	private static void dumpStrings(String title, String[] strings, int indent)
	{
		indent(indent);
		System.out.println(title);

		if (strings != null)
		{
			for (String string : strings)
			{
				indent(indent + 1);
				System.out.println(string);
			}
		}
		else
		{
			indent(indent + 1);
			System.out.println("(none)");
		}
	}

	private static void dumpBoolean(String title, boolean value, int indent)
	{
		indent(indent);
		System.out.print(title);
		System.out.println(value ? " YES" : " NO");
	}

	private static void dumpString(String title, String value, int indent)
	{
		indent(indent);
		System.out.print(title);
		System.out.print(" ");
		System.out.println(value != null ? value : "(null)");
	}

	private static void dumpReaderWriter(ImageReaderWriterSpi object)
	{
		indent(1);
		System.out.println(object.getPluginClassName());
		dumpStrings("File Suffixes:", object.getFileSuffixes(), 2);
		dumpStrings("Format Names:", object.getFormatNames(), 2);
		dumpStrings("MIME Types:", object.getMIMETypes(), 2);
		dumpBoolean("Standard Image Metadata Format Supported:",
					object.isStandardImageMetadataFormatSupported(),
					2);
		dumpBoolean("Standard Stream Metadata Format Supported:",
					object.isStandardStreamMetadataFormatSupported(),
					2);
		dumpString(	"Native Image Metadata Format Name:",
					object.getNativeImageMetadataFormatName(),
					2);
		dumpString(	"Native Stream Metadata Format Name:",
					object.getNativeStreamMetadataFormatName(),
					2);
		dumpStrings("Extra Image Metadata Format Names:",
					object.getExtraImageMetadataFormatNames(),
					2);
		dumpStrings("Extra Stream Metadata Format Names:",
					object.getExtraStreamMetadataFormatNames(),
					2);
		System.out.println("");
	}

	public static void main(String[] args)
	{
		dumpStrings("File Suffixes:", ImageIO.getReaderFileSuffixes(), 0);
		dumpStrings("\nFormat Names:", ImageIO.getReaderFormatNames(), 0);
		dumpStrings("\nMIME Types:", ImageIO.getReaderMIMETypes(), 0);

		IIORegistry registry = IIORegistry.getDefaultInstance();

		System.out.println("\nReaders:");
		Iterator<ImageReaderSpi> readers = registry.getServiceProviders(ImageReaderSpi.class, true);
		while (readers.hasNext())
		{
			ImageReaderSpi reader = readers.next();
			dumpReaderWriter(reader);
		}

		System.out.println("\nWriters:");
		Iterator<ImageWriterSpi> writers = registry.getServiceProviders(ImageWriterSpi.class, true);
		while (writers.hasNext())
		{
			ImageWriterSpi writer = writers.next();
			dumpReaderWriter(writer);
		}
	}
}
