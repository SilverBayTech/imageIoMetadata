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

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.Iterator;

import javax.imageio.IIOImage;
import javax.imageio.ImageIO;
import javax.imageio.ImageReader;
import javax.imageio.ImageTypeSpecifier;
import javax.imageio.ImageWriteParam;
import javax.imageio.ImageWriter;
import javax.imageio.metadata.IIOMetadata;
import javax.imageio.metadata.IIOMetadataFormatImpl;
import javax.imageio.metadata.IIOMetadataNode;
import javax.imageio.stream.ImageInputStream;
import javax.imageio.stream.ImageOutputStream;

public class ChangeImageResolution
{
	private static String getFileExtension(File file)
	{
		String fileName = file.getName();
		int lastDot = fileName.lastIndexOf('.');
		return fileName.substring(lastDot + 1);
	}

	private static BufferedImage readImage(File file) throws IOException
	{
		ImageInputStream stream = null;
		BufferedImage image = null;
		try
		{
			stream = ImageIO.createImageInputStream(file);
			Iterator<ImageReader> readers = ImageIO.getImageReaders(stream);
			if (readers.hasNext())
			{
				ImageReader reader = readers.next();
				reader.setInput(stream);
				image = reader.read(0);
			}
		}
		finally
		{
			if (stream != null)
			{
				stream.close();
			}
		}

		return image;
	}
	
	private static IIOMetadataNode createResolutionMetadata(double resolutionDPI)
	{
		String pixelSize = Double.toString(25.4 / resolutionDPI);
		
		IIOMetadataNode horizontal = new IIOMetadataNode("HorizontalPixelSize");
		horizontal.setAttribute("value", pixelSize);
		
		IIOMetadataNode vertical = new IIOMetadataNode("VerticalPixelSize");
		vertical.setAttribute("value", pixelSize);
		
		IIOMetadataNode dimension = new IIOMetadataNode("Dimension");
		dimension.appendChild(horizontal);
		dimension.appendChild(vertical);
		
		IIOMetadataNode root = new IIOMetadataNode(IIOMetadataFormatImpl.standardMetadataFormatName);
		root.appendChild(dimension);
		
		return root;
	}
	
	private static void writeImage(File outputFile, BufferedImage image, IIOMetadataNode newMetadata) throws IOException
	{
		String extension = getFileExtension(outputFile);
		ImageTypeSpecifier imageType = ImageTypeSpecifier.createFromBufferedImageType(image.getType());
		
		ImageOutputStream stream = null;
		try
		{
			Iterator<ImageWriter> writers = ImageIO.getImageWritersBySuffix(extension);
			while(writers.hasNext())
			{
				ImageWriter writer = writers.next();
				ImageWriteParam writeParam = writer.getDefaultWriteParam();
				IIOMetadata imageMetadata = writer.getDefaultImageMetadata(imageType, writeParam);
				if (!imageMetadata.isStandardMetadataFormatSupported())
				{
					continue;
				}
				if (imageMetadata.isReadOnly())
				{
					continue;
				}
				
				imageMetadata.mergeTree(IIOMetadataFormatImpl.standardMetadataFormatName, newMetadata);
				
				IIOImage imageWithMetadata = new IIOImage(image, null, imageMetadata);
				
				stream = ImageIO.createImageOutputStream(outputFile);
				writer.setOutput(stream);
				writer.write(null, imageWithMetadata, writeParam);
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

	public static void main(String[] args)
	{
		if (args.length != 3)
		{
			System.out
				.println("Usage: ChangeImageResolution inputFile newResolutionDPI outputFile");
			return;
		}

		try
		{
			File inputFile = new File(args[0]);
			double resolutionDPI = Double.parseDouble(args[1]);
			File outputFile = new File(args[2]);
			
			BufferedImage image = readImage(inputFile);
			IIOMetadataNode newMetadata = createResolutionMetadata(resolutionDPI);
			writeImage(outputFile, image, newMetadata);
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
	}
}
