import * as React from 'react';
import {Column} from '../../../components/layouts/column/Column';
import {useToggleVisibility} from '../../../hooks/toogleVisibilityHook';
import {ToolbarView} from '../../../state/ui/toolbar/toolbarModels';
import {LegendContainer} from '../containers/LegendContainer';
import {GraphContainer, MeasurementsContainer} from '../containers/SelectionReportContainer';
import {Props} from '../containers/SelectionReportContentContainer';
import {ToolbarContainer} from '../containers/ToolbarContainer';

const isTabVisible = (show: boolean): string => show ? 'flex' : 'none';

export const SelectionReport = ({
  addAllToSelectionReport,
  fetchLegendItems,
  isSuccessfullyFetched,
  legendItems,
  parameters,
  view
}: Props) => {
  React.useEffect(() => {
    fetchLegendItems(parameters);
  }, [parameters]);

  React.useEffect(() => {
    if (isSuccessfullyFetched && legendItems.length) {
      addAllToSelectionReport(legendItems);
    }
  }, [isSuccessfullyFetched]);

  const {isVisible, showHide} = useToggleVisibility(false);

  return (
    <Column>
      <ToolbarContainer showHideLegend={showHide}/>
      <Column style={{display: isTabVisible(view === ToolbarView.graph)}}>
        {view === ToolbarView.graph && <GraphContainer/>}
      </Column>
      <Column style={{display: isTabVisible(view === ToolbarView.table)}}>
        {view === ToolbarView.table && <MeasurementsContainer/>}
      </Column>
      {view === ToolbarView.graph && <LegendContainer isVisible={isVisible} showHideLegend={showHide}/>}
    </Column>
  );
};
