import * as classNames from 'classnames';
import * as React from 'react';
import {Link} from 'react-router-dom';
import {OnSelectPeriod} from '../../../../state/search/selection/selectionModels';
import {ClassNamed, Period} from '../../../../types/Types';
import {routes} from '../../../app/routes';
import {PeriodSelection} from '../../../common/components/dates/PeriodSelection';
import {Row, RowCenter} from '../../../common/components/layouts/row/Row';
import {Logo} from '../../../common/components/logo/Logo';
import {SummaryWrapper} from '../../../common/components/summary/SummaryWrapper';
import './SelectionMenuWrapper.scss';

interface Props extends ClassNamed {
  children?: React.ReactNode;
  period: Period;
  selectPeriod: OnSelectPeriod;
}

export const SearchMenuWrapper = (props: Props) => {
  const {children, className, period, selectPeriod} = props;

  return (
    <Row className={classNames('SelectionMenuWrapper', className)}>
      <Row className="SelectionMenu">
        {children}
      </Row>
      <Row>
        <Link className="Logo" to={routes.dashboard}>
          <Logo className="small"/>
        </Link>
      </Row>
      <RowCenter>
        <PeriodSelection selectPeriod={selectPeriod} period={period}/>
        <SummaryWrapper/>
      </RowCenter>
    </Row>
  );
};
