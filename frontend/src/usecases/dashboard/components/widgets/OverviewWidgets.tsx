import * as React from 'react';
import {Link} from 'react-router-dom';
import {routes} from '../../../../app/routes';
import {IndicatorWidget} from '../../../../components/indicators/IndicatorWidget';
import {Medium, WidgetModel} from '../../../../components/indicators/indicatorWidgetModels';
import {Column} from '../../../../components/layouts/column/Column';
import {translate} from '../../../../services/translationService';
import './OverviewWidgets.scss';
import {Widget} from './Widget';

interface Props {
  widgets: WidgetModel[];
}

export const OverviewWidgets = (props: Props) => {
  const {widgets} = props;

  const collectionWidget: WidgetModel = widgets.length
    ? widgets[0]
    : {
      type: Medium.collection,
      total: 0,
      pending: 0,
    };

  return (
    <Column className="OverviewWidgets">
      <Widget>
        <Column>
          <Link to={routes.collection}>
            <IndicatorWidget widget={collectionWidget} title={translate('collection')}/>
          </Link>
        </Column>
      </Widget>
    </Column>
  );
};
