import * as React from 'react';
import {Row} from '../../../../components/layouts/row/Row';
import {Normal} from '../../../../components/texts/Texts';
import {translate} from '../../../../services/translationService';
import './NoSavedSelections.scss';

export const NoSavedSelections = () => (
  <Row className="NoSavedSelections">
    <Normal className="Italic first-uppercase">{translate('no saved selections')}</Normal>
  </Row>
);
