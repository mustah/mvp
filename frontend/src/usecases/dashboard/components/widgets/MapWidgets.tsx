import * as React from 'react';
import {Row} from '../../../../components/layouts/row/Row';
import {DomainModel, GeoPosition} from '../../../../state/domain-models/domainModels';
import MapContainer, {PopupMode} from '../../../map/containers/MapContainer';
import {MapMarker} from '../../../map/mapModels';
import {Widget} from './Widget';
import ClusterContainer from '../../../map/containers/ClusterContainer';

interface Props {
  tmp: any;
}

export const MapWidgets = (props: Props) => {
  const {tmp} = props;

  // TODO retrieve real data
  const markers: DomainModel<MapMarker> = {};
  markers[0] = {
    status: {id: 3, name: 'Fel'},
    address: {id: '', cityId: '', name: ''},
    city: {id: '', name: ''},
    position: {
      confidence: 1,
      latitude: 56.138288,
      longitude: 13.394854,
    },
  };

  markers[1] = {
    status: {id: 3, name: 'Fel'},
    address: {id: '', cityId: '', name: ''},
    city: {id: '', name: ''},
    position: {
      confidence: 1,
      latitude: 56.552119,
      longitude: 14.137460,
    },
  };

  const centerOfPerstorpMap: GeoPosition = {latitude: 56.138288, longitude: 13.394854, confidence: 1};
  const centerOfErrorMap: GeoPosition = {latitude: 56.228288, longitude: 13.794854, confidence: 1};

  return (
    <Row className="MapWidgets">
      <Widget title="Perstorp">
        <MapContainer
          height={400}
          width={400}
          defaultZoom={13}
          viewCenter={centerOfPerstorpMap}
          popupMode={PopupMode.meterpoint}
        >
          <ClusterContainer markers={tmp}/>
        </MapContainer>
      </Widget>
      <Widget title="Fel">
        <MapContainer
          height={400}
          width={400}
          defaultZoom={8}
          viewCenter={centerOfErrorMap}
          popupMode={PopupMode.meterpoint}
        >
          <ClusterContainer markers={markers}/>
        </MapContainer>
      </Widget>
    </Row>
  );
};
