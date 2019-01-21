import * as React from 'react';
import {MeterDetailsContainer} from '../../containers/dialogs/MeterDetailsContainer';
import {Maybe} from '../../helpers/Maybe';
import {Meter} from '../../state/domain-models-paginated/meter/meterModels';
import {uuid} from '../../types/Types';
import {OpenDialogInfoButton} from '../dialog/OpenDialogInfoButton';

interface Props {
  meter: Meter;
}

const labelStyle: React.CSSProperties = {
  textOverflow: 'ellipsis',
  maxWidth: 170,
  whiteSpace: 'nowrap',
  overflow: 'hidden',
};

const iconStyle: React.CSSProperties = {
  height: 40,
  padding: 0,
};

export const MeterListItem = ({meter: {facility, id}}: Props) => (
  <OpenDialogInfoButton label={facility} autoScrollBodyContent={true} labelStyle={labelStyle} iconStyle={iconStyle}>
    <MeterDetailsContainer selectedId={Maybe.just<uuid>(id)}/>
  </OpenDialogInfoButton>
);
