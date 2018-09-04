import * as React from 'react';
import {MeterDetailsContainer} from '../../containers/dialogs/MeterDetailsContainer';
import {Maybe} from '../../helpers/Maybe';
import {Meter} from '../../state/domain-models-paginated/meter/meterModels';
import {uuid} from '../../types/Types';
import {OpenDialogInfoButton} from '../dialog/OpenDialogInfoButton';

interface Props {
  meter: Meter;
}

export const MeterListItem = ({meter}: Props) => {
  const selectedId = Maybe.just<uuid>(meter.id);
  return (
    <OpenDialogInfoButton label={meter.facility} autoScrollBodyContent={true}>
      <MeterDetailsContainer selectedId={selectedId}/>
    </OpenDialogInfoButton>
  );
};
