import {lifecycle} from 'recompose';
import {TabName} from '../../../state/ui/tabs/tabsModels';
import {MapMarkerProps} from '../mapModels';

const fetchMarkersWhenMapTabIsSelected =
  ({fetchMapMarkers, selectedTab, parameters}: MapMarkerProps) => {
    if (TabName.map === selectedTab) {
      fetchMapMarkers(parameters);
    }
  };

export const withMapMarkersFetcher = lifecycle<MapMarkerProps, {}>({

  componentDidMount() {
    fetchMarkersWhenMapTabIsSelected(this.props);
  },

  componentWillReceiveProps(props: MapMarkerProps) {
    fetchMarkersWhenMapTabIsSelected(props);
  },
});
