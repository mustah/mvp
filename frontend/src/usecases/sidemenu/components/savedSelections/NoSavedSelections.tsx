import 'NoSavedSelections.scss';
import * as React from 'react';
import {translate} from '../../../../services/translationService';
import {Row} from '../../../common/components/layouts/row/Row';
import {Normal} from '../../../common/components/texts/Texts';

export const NoSavedSelections = () => (
  <Row className="NoSavedSelections first-uppercase">
    <Normal className="Italic">{translate('no saved selections')}</Normal>
  </Row>
);
