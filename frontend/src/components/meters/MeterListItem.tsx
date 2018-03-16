import * as React from 'react';
import {MeterDetailsContainer} from '../../containers/dialogs/MeterDetailsContainer';
import {Meter} from '../../state/domain-models-paginated/meter/meterModels';
import {OpenDialogInfoButton} from '../dialog/OpenDialogInfoButton';

interface Props {
  meter: Meter;
}

export const MeterListItem = ({meter}: Props) => (
  <OpenDialogInfoButton label={meter.facility}>
    <MeterDetailsContainer meterId={meter.id}/>
  </OpenDialogInfoButton>
);
