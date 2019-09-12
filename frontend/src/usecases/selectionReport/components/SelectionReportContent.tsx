import * as React from 'react';
import {Column} from '../../../components/layouts/column/Column';
import {useToggleVisibility} from '../../../hooks/toogleVisibilityHook';
import {ToolbarView} from '../../../state/ui/toolbar/toolbarModels';
import {LegendContainer} from '../containers/LegendContainer';
import {SelectionMeasurementsExcelExportContainer} from '../containers/SelectionMeasurementsExcelExportContainer';
import {MeasurementLineContainer, MeasurementsContainer} from '../containers/SelectionReportContainers';
import {Props} from '../containers/SelectionReportContentContainer';
import {ToolbarContainer} from '../containers/ToolbarContainer';

const isTabVisible = (show: boolean): string => show ? 'flex' : 'none';

export const SelectionReportContent = ({
  addAllToSelectionReport,
  fetchLegendItems,
  isSuccessfullyFetched,
  legendItems,
  legendItemsParameters,
  view
}: Props) => {
  React.useEffect(() => {
    fetchLegendItems(legendItemsParameters);
  }, [legendItemsParameters]);

  React.useEffect(() => {
    if (isSuccessfullyFetched && legendItems.length) {
      addAllToSelectionReport(legendItems);
    }
  }, [isSuccessfullyFetched]);

  const {isVisible, showHide} = useToggleVisibility(false);

  return (
    <Column>
      <ToolbarContainer showHideLegend={showHide}/>

      <SelectionMeasurementsExcelExportContainer/>

      <Column style={{display: isTabVisible(view === ToolbarView.graph)}}>
        {view === ToolbarView.graph && <MeasurementLineContainer/>}
      </Column>

      <Column style={{display: isTabVisible(view === ToolbarView.table)}}>
        {view === ToolbarView.table && <MeasurementsContainer/>}
      </Column>

      {view === ToolbarView.graph && <LegendContainer isVisible={isVisible} showHideLegend={showHide}/>}
    </Column>
  );
};
