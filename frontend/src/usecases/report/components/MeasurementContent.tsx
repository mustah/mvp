import * as React from 'react';
import {Column} from '../../../components/layouts/column/Column';
import {useToggleVisibility} from '../../../hooks/toogleVisibilityHook';
import {ToolbarView} from '../../../state/ui/toolbar/toolbarModels';
import {LegendContainer} from '../containers/LegendContainer';
import {Props} from '../containers/MeasurementContentContainer';
import {GraphContainer, MeasurementsContainer} from '../containers/MeasurementsContainer';
import {ToolbarContainer} from '../containers/ToolbarContainer';

const display = (show: boolean): string => show ? 'flex' : 'none';

export const MeasurementContent = ({view}: Props) => {
  const {isVisible, showHide} = useToggleVisibility(false);
  return (
    <Column>
      <ToolbarContainer showHideLegend={showHide}/>
      <Column style={{display: display(view === ToolbarView.graph)}}>
        <GraphContainer/>
      </Column>
      <Column style={{display: display(view === ToolbarView.table)}}>
        <MeasurementsContainer/>
      </Column>
      <LegendContainer isVisible={isVisible} showHideLegend={showHide}/>
    </Column>
  );
};
