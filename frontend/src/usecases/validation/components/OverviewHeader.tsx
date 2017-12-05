import * as React from 'react';
import {TabOptionsProps} from '../../../components/tabs/components/TabOptions';
import {Row, RowRight} from '../../../components/layouts/row/Row';
import {Column, ColumnCenter} from '../../../components/layouts/column/Column';
import {translate} from '../../../services/translationService';
import './OverviewHeader.scss';

interface OverviewHeaderProps {
  children: React.ReactElement<TabOptionsProps>;
  header: string;
}

export const OverviewHeader = (props: OverviewHeaderProps) => {
  const {header, children} = props;
  return (
    <Row className="StatusControl">
      <Column>
        <h2 className="first-uppercase">{header}</h2>
      </Column>
      <Column className="flex-1"/>
      <ColumnCenter>
        <RowRight>
          <div className="first-uppercase">{translate('filter by status') + ':'}</div>
          {children}
        </RowRight>
      </ColumnCenter>
    </Row>
  );
};
