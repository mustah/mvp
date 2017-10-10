import * as React from 'react';
import {Column} from '../../../common/components/layouts/column/Column';
import {Row} from '../../../common/components/layouts/row/Row';
import {MenuSeparator} from '../separators/MenuSeparator';
import './MainLogo.scss';

export const MainLogo = (props) => (
  <Column className="MainLogo-container">
    <Row className="MainLogo Row-center">
      <img src="usecases/topmenu/components/mainlogo/elvaco-logo.png"/>
      <div className="MainLogo-title">mvp</div>
    </Row>
    <MenuSeparator/>
  </Column>
);
