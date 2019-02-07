import {default as classNames} from 'classnames';
import * as React from 'react';
import {Column} from '../../../components/layouts/column/Column';
import {useToggleVisibility} from '../../../hooks/toogleVisibilityHook';
import {ToolbarView} from '../../../state/ui/toolbar/toolbarModels';
import {LegendContainer} from '../containers/LegendContainer';
import {Props} from '../containers/MeasurementContentContainer';
import {GraphContainer, MeasurementsContainer} from '../containers/MeasurementsContainer';
import {ToolbarContainer} from '../containers/ToolbarContainer';
import './MeasurementContent.scss';

export const MeasurementContent = ({view}: Props) => {
  const {isVisible, showHide} = useToggleVisibility(false);
  return (
    <Column className="MeasurementContent">
      <ToolbarContainer toggleLegend={showHide}/>
      <Column className={classNames('LegendContainer', {isVisible})}>
        <LegendContainer/>
      </Column>
      {view === ToolbarView.graph && <GraphContainer/>}
      {view === ToolbarView.table && <MeasurementsContainer/>}
    </Column>
  );
};
