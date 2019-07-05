import * as React from 'react';
import {Column} from '../../../components/layouts/column/Column';
import {useToggleVisibility} from '../../../hooks/toogleVisibilityHook';
import {ToolbarView, ToolbarViewSettingsProps} from '../../../state/ui/toolbar/toolbarModels';
import {LegendContainer} from '../containers/LegendContainer';
import {GraphContainer, MeasurementsContainer} from '../containers/MeasurementsContainer';
import {ReportMeasurementsExcelExportContainer} from '../containers/ReportMeasurementsExcelExportContainer';
import {ToolbarContainer} from '../containers/ToolbarContainer';

const display = (show: boolean): string => show ? 'flex' : 'none';

export const MeasurementContent = ({view}: ToolbarViewSettingsProps) => {
  const {isVisible, showHide} = useToggleVisibility(false);
  return (
    <Column>
      <ToolbarContainer showHideLegend={showHide}/>

      <ReportMeasurementsExcelExportContainer/>

      <Column style={{display: display(view === ToolbarView.graph)}}>
        {view === ToolbarView.graph && <GraphContainer/>}
      </Column>

      <Column style={{display: display(view === ToolbarView.table)}}>
        {view === ToolbarView.table && <MeasurementsContainer/>}
      </Column>

      {view === ToolbarView.graph && <LegendContainer isVisible={isVisible} showHideLegend={showHide}/>}
    </Column>
  );
};
