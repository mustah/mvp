import * as React from 'react';
import {routes} from '../../../../app/routes';
import {border} from '../../../../app/themes';
import {ButtonAdd} from '../../../../components/buttons/ButtonAdd';
import {Column} from '../../../../components/layouts/column/Column';
import {Row} from '../../../../components/layouts/row/Row';
import {Link} from '../../../../components/links/Link';
import {translate} from '../../../../services/translationService';
import {DevicesGridContainer} from '../containers/DevicesGridContainer';

export const DevicesContent = () => (
  <Column>
    <Row>
      <Link to={routes.otcDevicesAdd} key="add device">
        <ButtonAdd label={translate('add device')}/>
      </Link>
    </Row>
    <Row style={{borderTop: border}}>
      <DevicesGridContainer/>
    </Row>
  </Column>
);
