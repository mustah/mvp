import * as React from 'react';
import {routes} from '../../../../app/routes';
import {ButtonAdd} from '../../../../components/buttons/ButtonAdd';
import {Column} from '../../../../components/layouts/column/Column';
import {Row} from '../../../../components/layouts/row/Row';
import {Link} from '../../../../components/links/Link';
import {translate} from '../../../../services/translationService';

export const DeviceList = () => (
  <Column>
    <Row>
      <Link to={routes.otcDevicesAdd} key="add device">
        <ButtonAdd label={translate('add device')}/>
      </Link>
    </Row>
  </Column>
);
