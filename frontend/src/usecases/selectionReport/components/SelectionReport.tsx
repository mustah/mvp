import * as React from 'react';
import {Column} from '../../../components/layouts/column/Column';
import {useToggleVisibility} from '../../../hooks/toogleVisibilityHook';
import {ToolbarView} from '../../../state/ui/toolbar/toolbarModels';
import {LegendContainer} from '../containers/LegendContainer';
import {GraphContainer, MeasurementsContainer} from '../containers/SelectionReportContainer';
import {Props} from '../containers/SelectionReportContentContainer';
import {ToolbarContainer} from '../containers/ToolbarContainer';

const isTabVisible = (show: boolean): string => show ? 'flex' : 'none';

export const SelectionReport = ({isFetching, legendItems, newLegendItems, addAllToSelectionReport, view}: Props) => {
  React.useEffect(() => {
    if (newLegendItems.length && newLegendItems.length !== legendItems.length) {
      addAllToSelectionReport(newLegendItems);
    }
  }, [legendItems, isFetching]);
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
