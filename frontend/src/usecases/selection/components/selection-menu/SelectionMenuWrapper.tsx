import * as classNames from 'classnames';
import * as React from 'react';
import {OnSelectPeriod} from '../../../../state/search/selection/selectionModels';
import {ClassNamed, Period} from '../../../../types/Types';
import {PeriodSelection} from '../../../common/components/dates/PeriodSelection';
import {Row, RowCenter} from '../../../common/components/layouts/row/Row';
import {Logo} from '../../../common/components/logo/Logo';
import {SummaryWrapper} from '../../../common/components/summary/SummaryWrapper';
import './SelectionMenu.scss';

interface Props extends ClassNamed {
  children?: React.ReactNode;
  period: Period;
  selectPeriod: OnSelectPeriod;
}

export const SearchMenuWrapper = (props: Props) => {
  const {children, className, period, selectPeriod} = props;

  return (
    <Row className={classNames('SelectionMenu-Container', className)}>
      <Row className="SelectionMenu">
        {children}
      </Row>
      <Row>
        <Logo className="small"/>
      </Row>
      <RowCenter>
        <PeriodSelection selectPeriod={selectPeriod} period={period}/>
        <SummaryWrapper/>
      </RowCenter>
    </Row>
  );
};
