import * as React from 'react';
import classNames = require('classnames');
import {Row} from '../../../usecases/layouts/components/row/Row';
import {Icon} from '../../common/components/icons/Icons';
import './SelectionsOverview.scss';

export const SelectionsOverview = props => (
  <Row>
    <div className="SelectionsOverview">
      Urval: {props.title}
    </div>
    <Icon name="star" size="small"/>
    <div className={classNames('Row Row-center LinkItem')}>{name}</div>
    <Icon name="chevron-left" className="Row-right flex-1" size="small"/>
  </Row>
);
