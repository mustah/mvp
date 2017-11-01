import * as classNames from 'classnames';
import * as React from 'react';
import {ClassNamed} from '../../../../types/Types';
import {PeriodSelection} from '../../../common/components/dates/PeriodSelection';
import {Row, RowCenter} from '../../../common/components/layouts/row/Row';
import {Logo} from '../../../common/components/logo/Logo';
import {SummaryWrapper} from '../../../common/components/summary/SummaryWrapper';
import './SelectionMenu.scss';

interface Props extends ClassNamed {
  children?: React.ReactNode;
}

export const SearchMenuWrapper = (props: Props) => (
  <Row className={classNames('SelectionMenu-Container', props.className)}>
    <Row className="SelectionMenu">
      {props.children}
    </Row>
    <Row>
      <Logo className="small"/>
    </Row>
    <RowCenter>
      <PeriodSelection/>
      <SummaryWrapper/>
    </RowCenter>
  </Row>
);
