import * as React from 'react';
import {withSmallLoader} from '../../../../components/hoc/withLoaders';
import {IndicatorWidget} from '../../../../components/indicators/IndicatorWidget';
import {WidgetModel} from '../../../../components/indicators/indicatorWidgetModels';
import {Column, ColumnCenter} from '../../../../components/layouts/column/Column';
import {translate} from '../../../../services/translationService';
import './OverviewWidgets.scss';
import {EmptyWidget, Widget} from './Widget';

interface Props {
  isFetching: boolean;
  widgets: WidgetModel[];
}

interface WidgetProps {
  widget: WidgetModel;
}

const emptyWidget: WidgetModel = {
  collectionPercentage: NaN,
};

const containerStyle: React.CSSProperties = {height: 108, display: 'flex'};

const IndicatorContent = ({widget}: WidgetProps) => (
  <Column>
    <IndicatorWidget widget={widget} title={translate('collection')}/>
  </Column>
);

const LoadingIndicator = withSmallLoader<WidgetProps>(IndicatorContent);

export const OverviewWidgets = ({isFetching, widgets}: Props) => {
  const widget: WidgetModel = widgets.length > 0 ? widgets[0] : emptyWidget;

  return (
    <Column className="OverviewWidgets">
      <Widget containerStyle={containerStyle}>
        <ColumnCenter className="flex-1">
          <LoadingIndicator isFetching={isFetching} widget={widget}/>
        </ColumnCenter>
      </Widget>
      <EmptyWidget style={{height: 90}}/>
    </Column>
  );
};
