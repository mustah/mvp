import * as React from 'react';
import {Xlarge} from '../../common/components/texts/Texts';
import {PeriodSelectionContainer} from '../../common/containers/PeriodSelectionContainer';
import {Row} from '../../layouts/components/row/Row';
import {ColoredBox} from '../../widget/containers/coloredBox/ColoredBox';
import {DonutGraph} from '../../widget/containers/donutGraph/DonutGraph';
import {ColoredBoxModel as ColoredBoxModel} from '../../widget/models/ColoredBoxModel';
import {DonutGraphModel as DonutGraphModel} from '../../widget/models/DonutGraphModel';
import {WidgetModel} from '../../widget/models/WidgetModel';
import {SystemOverviewState} from '../types';

interface SystemOverviewProps {
  overview: SystemOverviewState;
}

export const SystemOverviewContainer = (props: SystemOverviewProps) => {
  const {overview} = props;

  const renderWidget = (widget: WidgetModel, index: number) => {
    if (widget instanceof ColoredBoxModel) {
      return <ColoredBox key={index} {...widget as ColoredBoxModel}/>;
    }

    if (widget instanceof DonutGraphModel) {
      return <DonutGraph key={index} {...widget as DonutGraphModel}/>;
    }

    return null;
  };

  return (
    <div>
      <Row>
        <Xlarge className="Bold">{overview.title}</Xlarge>
      </Row>
      <Row className="Row-right">
        <PeriodSelectionContainer/>
      </Row>
      <Row>
        {overview.widgets.map(renderWidget)}
      </Row>
    </div>
  );
};
