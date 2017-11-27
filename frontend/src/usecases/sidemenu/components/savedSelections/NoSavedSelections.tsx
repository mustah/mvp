import 'NoSavedSelections.scss';
import * as React from 'react';
import {translate} from '../../../../services/translationService';
import {Row} from '../../../../components/layouts/row/Row';
import {Normal} from '../../../../components/texts/Texts';

export const NoSavedSelections = () => (
  <Row className="NoSavedSelections">
    <Normal className="Italic first-uppercase">{translate('no saved selections')}</Normal>
  </Row>
);
