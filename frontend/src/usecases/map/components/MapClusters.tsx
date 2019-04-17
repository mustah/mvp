import * as React from 'react';
import {ClusterContainer} from '../containers/ClusterContainer';
import {MapProps} from '../mapModels';
import {ResponsiveMap} from './Map';

export const MapClusters = ({bounds, key, lowConfidenceText, mapMarkers}: MapProps) => (
  <ResponsiveMap bounds={bounds} lowConfidenceText={lowConfidenceText} paddingBottom={270} key={key}>
    <ClusterContainer markers={mapMarkers.entities}/>
  </ResponsiveMap>
);
