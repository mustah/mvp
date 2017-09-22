import * as React from 'react';
import {Xlarge} from '../../common/components/texts/Texts';
import {PeriodSelectionContainer} from '../../common/containers/PeriodSelectionContainer';
import {Row} from '../../layouts/components/row/Row';
import {SystemOverviewState} from '../types';
import {ColoredBoxModel, DonutGraphModel, WidgetModel} from '../../widget/models/WidgetModels';
import {ColoredBox} from '../../widget/containers/coloredBox/ColoredBox';
import {DonutGraph} from '../../widget/containers/donutGraph/DonutGraph';

interface SystemOverviewProps {
  overview: SystemOverviewState,
};

export const SystemOverviewContainer = (props: SystemOverviewProps) => {
  const {overview} = props;
  return (
    <div>
      <Row>
        <Xlarge className="Bold">{overview.title}</Xlarge>
      </Row>
      <Row className="Row-right">
        <PeriodSelectionContainer/>
      </Row>
      <Row>
        {overview.widgets.map((widget: WidgetModel, index: number) => {
          let rec;
          if(widget instanceof ColoredBoxModel) {
            rec = <ColoredBox key={index} {...widget as ColoredBoxModel}/>;
          } else if(widget instanceof DonutGraphModel) {
            rec = <DonutGraph key={index} {...widget as DonutGraphModel}/>;
          }
          return rec;
        })}
      </Row>
    </div>
  );
};
