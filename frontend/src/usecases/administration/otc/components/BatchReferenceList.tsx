import * as React from 'react';
import {routes} from '../../../../app/routes';
import {ButtonAdd} from '../../../../components/buttons/ButtonAdd';
import {Column} from '../../../../components/layouts/column/Column';
import {Row} from '../../../../components/layouts/row/Row';
import {Link} from '../../../../components/links/Link';
import {translate} from '../../../../services/translationService';

export const BatchReferenceList = () => (
  <Column>
    <Row>
      <Link to={routes.otcBatchReferencesCreate} key="create batch reference">
        <ButtonAdd label={translate('create batch reference')}/>
      </Link>
    </Row>
  </Column>
);
