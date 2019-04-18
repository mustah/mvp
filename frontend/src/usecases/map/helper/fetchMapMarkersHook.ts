import * as React from 'react';
import {TabName} from '../../../state/ui/tabs/tabsModels';
import {MapMarkerProps} from '../mapModels';

export const useFetchMapMarkers = ({fetchMapMarkers, selectedTab, parameters}: MapMarkerProps) => {
  React.useEffect(() => {
    if (TabName.map === selectedTab) {
      fetchMapMarkers(parameters);
    }
  }, [selectedTab, parameters]);
};
