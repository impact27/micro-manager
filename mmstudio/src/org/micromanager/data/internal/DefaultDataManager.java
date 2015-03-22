///////////////////////////////////////////////////////////////////////////////
//PROJECT:       Micro-Manager
//SUBSYSTEM:     Data API implementation
//-----------------------------------------------------------------------------
//
// AUTHOR:       Chris Weisiger, 2015
//
// COPYRIGHT:    University of California, San Francisco, 2015
//
// LICENSE:      This file is distributed under the BSD license.
//               License text is included with the source distribution.
//
//               This file is distributed in the hope that it will be useful,
//               but WITHOUT ANY WARRANTY; without even the implied warranty
//               of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
//
//               IN NO EVENT SHALL THE COPYRIGHT OWNER OR
//               CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT,
//               INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES.

package org.micromanager.data.internal;

import java.io.IOException;

import mmcorej.TaggedImage;

import org.json.JSONException;

import org.micromanager.data.DataManager;
import org.micromanager.data.Datastore;
import org.micromanager.data.Image;
import org.micromanager.data.Metadata;
import org.micromanager.data.SummaryMetadata;

import org.micromanager.data.Coords;
import org.micromanager.data.internal.multipagetiff.MultipageTiffReader;
import org.micromanager.data.internal.multipagetiff.StorageMultipageTiff;
import org.micromanager.data.internal.StorageRAM;
import org.micromanager.data.internal.StorageSinglePlaneTiffSeries;
import org.micromanager.events.internal.DefaultEventManager;

import org.micromanager.internal.MMStudio;
// TODO: this should be moved into the API.
import org.micromanager.internal.utils.MMScriptException;
import org.micromanager.PropertyMap;

/**
 * This implementation of the DataManager interface provides general utility
 * access to Micro-Manager's data objects.
 */
public class DefaultDataManager implements DataManager {
   private MMStudio studio_;

   public DefaultDataManager(MMStudio studio) {
      studio_ = studio;
      DefaultEventManager.getInstance().registerForEvents(this);
   }

   @Override
   public Coords.CoordsBuilder getCoordsBuilder() {
      return new DefaultCoords.Builder();
   }

   @Override
   public Datastore createRAMDatastore() {
      Datastore result = new DefaultDatastore();
      result.setStorage(new StorageRAM(result));
      return result;
   }

   @Override
   public Datastore createMultipageTIFFDatastore(String directory,
         boolean shouldGenerateSeparateMetadata, boolean shouldSplitPositions)
         throws IOException {
      DefaultDatastore result = new DefaultDatastore();
      result.setStorage(new StorageMultipageTiff(result, directory, true,
               shouldGenerateSeparateMetadata, shouldSplitPositions));
      return result;
   }

   @Override
   public Datastore createSinglePlaneTIFFSeriesDatastore(String directory) {
      DefaultDatastore result = new DefaultDatastore();
      result.setStorage(new StorageSinglePlaneTiffSeries(result, directory,
            true));
      return result;
   }

   @Override
   public Datastore loadData(String directory, boolean isVirtual) throws IOException {
      DefaultDatastore result = new DefaultDatastore();
      // TODO: future additional file formats will need to be handled here.
      // For now we just choose between StorageMultipageTiff and
      // StorageSinglePlaneTiffSeries.
      boolean isMultipageTiff = MultipageTiffReader.isMMMultipageTiff(directory);
      if (isMultipageTiff) {
         result.setStorage(new StorageMultipageTiff(result, directory, false));
      }
      else {
         result.setStorage(new StorageSinglePlaneTiffSeries(result, directory,
                  false));
      }
      if (!isVirtual) {
         // Copy into a StorageRAM.
         DefaultDatastore tmp = (DefaultDatastore) createRAMDatastore();
         tmp.copyFrom(result);
         result = tmp;
      }
      return result;
   }

   @Override
   public Image createImage(Object pixels, int width, int height,
         int bytesPerPixel, int numComponents, Coords coords,
         Metadata metadata) {
      return new DefaultImage(pixels, width, height, bytesPerPixel,
            numComponents, coords, metadata);
   }

   @Override
   public Image convertTaggedImage(TaggedImage tagged) throws JSONException, MMScriptException {
      return new DefaultImage(tagged);
   }

   @Override
   public Image convertTaggedImage(TaggedImage tagged, Coords coords,
         Metadata metadata) throws JSONException, MMScriptException {
      return new DefaultImage(tagged, coords, metadata);
   }

   @Override
   public Metadata.MetadataBuilder getMetadataBuilder() {
      return new DefaultMetadata.Builder();
   }

   @Override
   public SummaryMetadata.SummaryMetadataBuilder getSummaryMetadataBuilder() {
      return new DefaultSummaryMetadata.Builder();
   }

   @Override
   public PropertyMap.PropertyMapBuilder getPropertyMapBuilder() {
      return new DefaultPropertyMap.Builder();
   }
}
