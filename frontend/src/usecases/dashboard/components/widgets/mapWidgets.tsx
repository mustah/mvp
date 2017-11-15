import * as React from 'react';
import {Row} from '../../../common/components/layouts/row/Row';
import {Widget} from './Widget';
import MapContainer, {PopupMode} from '../../../map/containers/MapContainer';
import {MapMarker} from '../../../map/mapModels';

interface Props {
  tmp: any;
}

export const MapWidgets = (props: Props) => {
  const {tmp} = props;

  // TODO retrieve real data
  const markers: { [key: string]: MapMarker } = {};
  markers[0] = {
    status: {id: 3, name: 'Fel'},
    address: {id: '', cityId: '', name: ''},
    city: {id: '', name: ''},
    position: {
      confidence: 1,
      latitude: '56.138288',
      longitude: '13.394854',
    },
  };

  markers[1] = {
    status: {id: 3, name: 'Fel'},
    address: {id: '', cityId: '', name: ''},
    city: {id: '', name: ''},
    position: {
      confidence: 1,
      latitude: '56.552119',
      longitude: '14.137460',
    },
  };

  const centerOfPerstorpMap: [number, number] = [56.138288, 13.394854];
  const centerOfErrorMap: [number, number] = [56.228288, 13.794854];

  return (
    <Row className="MapWidgets">
      <Widget title="Perstorp">
        <MapContainer
          height={400}
          width={400}
          markers={tmp}
          defaultZoom={13}
          viewCenter={centerOfPerstorpMap}
          popupMode={PopupMode.meterpoint}
        />
      </Widget>
      <Widget title="Fel">
        <MapContainer
          height={400}
          width={400}
          markers={markers}
          defaultZoom={8}
          viewCenter={centerOfErrorMap}
          popupMode={PopupMode.meterpoint}
        />
      </Widget>
    </Row>
  );
};
