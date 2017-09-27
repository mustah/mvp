import * as classNames from 'classnames';
import * as React from 'react';
import {Link} from 'react-router-dom';
import {Bold, Normal} from '../../../common/components/texts/Texts';
import {Column} from '../../../layouts/components/column/Column';
import {Row} from '../../../layouts/components/row/Row';
import {ColoredBoxModel as ColoredBoxModel} from '../../models/ColoredBoxModel';
import './ColoredBox.scss';

export const ColoredBox = (props: ColoredBoxModel) => {
  const {state, title, value, unit, subtitle, url} = props;
  return (
    <Link to={url} className="link">
      <Column className={classNames('ColoredBox Column-center', state)}>
        <Row className={classNames('Row-center ColoredBox-name')}>
          <Normal>{title}</Normal>
        </Row>
        <Row className={classNames('Row-center ColoredBox-value')}>
          <Bold>{value}</Bold>
        </Row>
        <Row className={classNames('Row-center ColoredBox-unit')}>
          <Bold>{unit}</Bold>
        </Row>
        <Row className={classNames('Row-center ColoredBox-subtitle')}>
          <Bold>{subtitle}</Bold>
        </Row>
      </Column>
    </Link>
  );
};
