import * as React from 'react';
import {routes} from '../../../../app/routes';
import {border} from '../../../../app/themes';
import {ButtonAdd} from '../../../../components/buttons/ButtonAdd';
import {Column} from '../../../../components/layouts/column/Column';
import {Row} from '../../../../components/layouts/row/Row';
import {Link} from '../../../../components/links/Link';
import {translate} from '../../../../services/translationService';
import {BatchReferenceGridContainer} from '../containers/BatchReferenceGridContainer';

export const BatchReferenceContent = () => (
  <Column>
    <Row>
      <Link to={routes.otcBatchReferencesCreate} key="create batch reference">
        <ButtonAdd label={translate('create batch reference')}/>
      </Link>
    </Row>
    <Row style={{borderTop: border}}>
      <BatchReferenceGridContainer/>
    </Row>
  </Column>
);
