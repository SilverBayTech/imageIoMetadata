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

import java.io.File;
import java.io.IOException;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.Iterator;

import javax.imageio.ImageIO;
import javax.imageio.ImageReader;
import javax.imageio.metadata.IIOMetadata;
import javax.imageio.metadata.IIOMetadataFormatImpl;
import javax.imageio.spi.ImageReaderSpi;
import javax.imageio.stream.ImageInputStream;

import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

public class GetImageResolution
{
	private static final NumberFormat FORMAT = new DecimalFormat("#0.0");

	private static String getFileExtension(File file)
	{
		String fileName = file.getName();
		int lastDot = fileName.lastIndexOf('.');
		return fileName.substring(lastDot + 1);
	}

	private static Element getChildElement(Node parent, String name)
	{
		NodeList children = parent.getChildNodes();
		int count = children.getLength();
		for (int i = 0; i < count; i++)
		{
			Node child = children.item(i);
			if (child.getNodeType() == Node.ELEMENT_NODE)
			{
				if (child.getNodeName().equals(name))
				{
					return (Element)child;
				}
			}
		}

		return null;
	}

	private static void dumpResolution(String title, Element element)
	{
		System.out.print(title);
		if (element == null)
		{
			System.out.println("(none)");
			return;
		}

		String value = element.getAttribute("value");
		if (value == null)
		{
			System.out.println("(none)");
			return;
		}

		double mmPerPixel = Double.parseDouble(value);
		double pixelsPerInch = 25.4 / mmPerPixel;

		System.out.print(FORMAT.format(pixelsPerInch));
		System.out.println(" pixels per inch");
	}

	private static void processFileWithReader(File file, ImageReader reader) throws IOException
	{
		ImageInputStream stream = null;

		try
		{
			stream = ImageIO.createImageInputStream(file);

			reader.setInput(stream, true);

			IIOMetadata metadata = reader.getImageMetadata(0);

			Node root = metadata.getAsTree(IIOMetadataFormatImpl.standardMetadataFormatName);
			Element dimension = getChildElement(root, "Dimension");
			if (dimension != null)
			{
				Element horizontalPixelSize = getChildElement(dimension, "HorizontalPixelSize");
				Element verticalPixelSize = getChildElement(dimension, "VerticalPixelSize");

				dumpResolution("    Horizontal resolution: ", horizontalPixelSize);
				dumpResolution("    Vertical resolution: ", verticalPixelSize);
			}
		}
		finally
		{
			if (stream != null)
			{
				stream.close();
			}
		}
	}

	private static void processFile(File file) throws IOException
	{
		System.out.println("\nProcessing " + file.getName() + ":\n");

		String extension = getFileExtension(file);

		Iterator<ImageReader> readers = ImageIO.getImageReadersByFormatName(extension);

		while (readers.hasNext())
		{
			ImageReader reader = readers.next();

			ImageReaderSpi spi = reader.getOriginatingProvider();

			if (spi.isStandardImageMetadataFormatSupported())
			{
				processFileWithReader(file, reader);
				return;
			}
		}

		System.out.println("    No compatible reader found");
	}

	private static void processDirectory(File directory) throws IOException
	{
		System.out.println("Processing all files in " + directory.getAbsolutePath());

		File[] contents = directory.listFiles();
		for (File file : contents)
		{
			if (file.isFile())
			{
				processFile(file);
			}
		}
	}

	public static void main(String[] args)
	{
		try
		{
			for (int i = 0; i < args.length; i++)
			{
				File fileOrDirectory = new File(args[i]);

				if (fileOrDirectory.isFile())
				{
					processFile(fileOrDirectory);
				}
				else
				{
					processDirectory(fileOrDirectory);
				}
			}

			System.out.println("\nDone");
		}
		catch (IOException e)
		{
			e.printStackTrace();
		}
	}
}
