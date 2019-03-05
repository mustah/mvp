import * as React from 'react';
import {Column} from '../../../../components/layouts/column/Column';
import {ToolbarView} from '../../../../state/ui/toolbar/toolbarModels';
import {Props} from '../containers/MeasurementsContainer';
import {MeasurementToolbarContainer} from '../containers/MeasurementToolbarContainer';
import {MeterMeasurementsContainer} from '../containers/MeterMeasurementsContainer';

const isVisible = (show: boolean): string => show ? 'flex' : 'none';

export const MeasurementContent = ({view, meter}: Props) => (
  <Column>
    <MeasurementToolbarContainer/>
    <Column style={{display: isVisible(view === ToolbarView.table)}}>
      <MeterMeasurementsContainer meter={meter} />
    </Column>
  </Column>
);
