import 'OverviewWidgets.scss';
import * as React from 'react';
import {Link} from 'react-router-dom';
import {translate} from '../../../../services/translationService';
import {routes} from '../../../app/routes';
import {IndicatorWidget} from '../../../common/components/indicators/IndicatorWidget';
import {WidgetModel} from '../../../common/components/indicators/models/widgetModels';
import {Row} from '../../../common/components/layouts/row/Row';
import {Widget} from './Widget';

interface Props {
  widgets: WidgetModel[];
}

export const OverviewWidgets = (props: Props) => {
  const {widgets} = props;
  const collectionWidget = widgets[0];
  const validationWidget = widgets[1];

  return (
    <Row className="OverviewWidgets">
      <Widget title={translate('collection')}>
        <Link to={routes.collection}>
          <IndicatorWidget widget={collectionWidget}/>
        </Link>
      </Widget>
      <Widget title={translate('validation')}>
        <Link to={routes.validation}>
          <IndicatorWidget widget={validationWidget}/>
        </Link>
      </Widget>
    </Row>
  );
};
