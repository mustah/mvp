import * as React from 'react';
import {ClusterContainer} from '../containers/ClusterContainer';
import {MapProps} from '../mapModels';
import {Map} from './Map';

export const MapClusters = ({bounds, lowConfidenceText, mapMarkers}: MapProps) => (
  <Map bounds={bounds} lowConfidenceText={lowConfidenceText}>
    <ClusterContainer markers={mapMarkers.entities}/>
  </Map>
);
