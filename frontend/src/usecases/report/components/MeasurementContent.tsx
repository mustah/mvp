import * as React from 'react';
import {Column} from '../../../components/layouts/column/Column';
import {ToolbarView} from '../../../state/ui/toolbar/toolbarModels';
import {Props} from '../containers/MeasurementContentContainer';
import {GraphContainer, MeasurementsContainer} from '../containers/MeasurementsContainer';
import {ToolbarContainer} from '../containers/ToolbarContainer';

export const MeasurementContent = ({view}: Props) => (
  <Column>
    <ToolbarContainer/>
    {view === ToolbarView.graph && <GraphContainer/>}
    {view === ToolbarView.table && <MeasurementsContainer/>}
  </Column>
);
