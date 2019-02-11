import * as React from 'react';
import {Column} from '../../../components/layouts/column/Column';
import {ToolbarView} from '../../../state/ui/toolbar/toolbarModels';
import {Props} from '../containers/MeasurementContentContainer';
import {GraphContainer, MeasurementsContainer} from '../containers/MeasurementsContainer';
import {ToolbarContainer} from '../containers/ToolbarContainer';

const isVisible = (show: boolean): string => show ? 'flex' : 'none';

export const MeasurementContent = ({view}: Props) => (
  <Column>
    <ToolbarContainer/>
    <Column style={{display: isVisible(view === ToolbarView.graph)}}>
      <GraphContainer/>
    </Column>
    <Column style={{display: isVisible(view === ToolbarView.table)}}>
      <MeasurementsContainer/>
    </Column>
  </Column>
);
