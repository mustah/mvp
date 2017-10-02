import * as React from 'react';
import {PeriodSelection} from '../../../common/components/dates/PeriodSelection';
import {Xlarge} from '../../../common/components/texts/Texts';
import {Row} from '../../../layouts/components/row/Row';
import {ColoredBox} from '../../../widget/components/coloredBox/ColoredBox';
import {DonutGraph} from '../../../widget/components/donutGraph/DonutGraph';
import {ColoredBoxModel as ColoredBoxModel} from '../../../widget/models/ColoredBoxModel';
import {DonutGraphModel as DonutGraphModel} from '../../../widget/models/DonutGraphModel';
import {WidgetModel} from '../../../widget/models/WidgetModel';
import {SystemOverviewState} from '../../models/dashboardModels';

interface SystemOverviewProps {
  overview: SystemOverviewState;
}

export const SystemOverview = (props: SystemOverviewProps) => {
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
        <PeriodSelection/>
      </Row>
      <Row>
        {overview.widgets.map(renderWidget)}
      </Row>
    </div>
  );
};
