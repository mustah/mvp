import * as React from 'react';
import {Link} from 'react-router-dom';
import {routes} from '../../../../app/routes';
import {withSmallLoader} from '../../../../components/hoc/withLargeLoader';
import {IndicatorWidget} from '../../../../components/indicators/IndicatorWidget';
import {Medium, WidgetModel} from '../../../../components/indicators/indicatorWidgetModels';
import {Column, ColumnCenter} from '../../../../components/layouts/column/Column';
import {translate} from '../../../../services/translationService';
import './OverviewWidgets.scss';
import {Widget} from './Widget';

interface Props {
  isFetching: boolean;
  widgets: WidgetModel[];
}

interface WidgetProps {
  widget: WidgetModel;
}

const emptyWidget = {
  type: Medium.collection,
  total: 0,
  pending: 0,
};

const containerStyle: React.CSSProperties = {height: 144, display: 'flex'};

const IndicatorContent = ({widget}: WidgetProps) => (
  <Column>
    <Link to={routes.collection}>
      <IndicatorWidget widget={widget} title={translate('collection')}/>
    </Link>
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
    </Column>
  );
};
