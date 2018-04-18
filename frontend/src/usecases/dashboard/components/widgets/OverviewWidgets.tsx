import * as React from 'react';
import {Link} from 'react-router-dom';
import {routes} from '../../../../app/routes';
import {IndicatorWidget} from '../../../../components/indicators/IndicatorWidget';
import {IndicatorType, WidgetModel} from '../../../../components/indicators/indicatorWidgetModels';
import {ColumnContent} from '../../../../components/layouts/column/Column';
import {Row} from '../../../../components/layouts/row/Row';
import {translate} from '../../../../services/translationService';
import {Status} from '../../../../types/Types';
import './OverviewWidgets.scss';
import {Widget} from './Widget';

interface Props {
  widgets: WidgetModel[];
}

export const OverviewWidgets = (props: Props) => {
  const {widgets} = props;

  const collectionWidget: WidgetModel = widgets.length > 0 ? widgets[0] : {
    type: IndicatorType.collection,
    total: 0,
    status: Status.ok,
    pending: 0,
  };

  return (
    <Row className="OverviewWidgets">
      <Widget title={translate('collection')}>
        <ColumnContent>
          <Link to={routes.collection}>
            <IndicatorWidget widget={collectionWidget}/>
          </Link>
        </ColumnContent>
      </Widget>
    </Row>
  );
};
