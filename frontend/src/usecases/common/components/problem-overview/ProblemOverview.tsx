import * as React from 'react';
import {translate} from '../../../../services/translationService';
import {Table} from '../table/table/Table';
import {TableHead} from '../table/table/TableHead';
import {TableColumn} from '../table/tableColumn/TableColumn';
import {Column} from '../layouts/column/Column';
import {Layout} from '../layouts/layout/Layout';
import {Row} from '../layouts/row/Row';
import {Bold} from '../texts/Texts';
import './ProblemOverview.scss';
import {Category} from '../../../collection/models/Collections';

interface ProblemOverviewProps {
  categories: Category;
}

export const ProblemOverview = (props: ProblemOverviewProps) => {
  const {categories: {handled, unhandled}} = props;
  return (
    <Layout>
      <Row className="ProblemOverview">
        <Column className="ProblemOverview-grouping">
          <Bold className="ProblemOverview-title">{translate('unhandled problems')}</Bold>
          <Table data={unhandled}>
            <TableColumn header={<TableHead>{translate('grouping')}</TableHead>} id="category"/>
            <TableColumn header={<TableHead>{translate('count')}</TableHead>} id="count"/>
          </Table>
        </Column>
        <Column className="ProblemOverview-grouping">
          <Bold className="ProblemOverview-title">{translate('action pending')}</Bold>
          <Table data={handled}>
            <TableColumn header={<TableHead>{translate('grouping')}</TableHead>} id="category"/>
            <TableColumn header={<TableHead>{translate('count')}</TableHead>} id="count"/>
          </Table>
        </Column>
      </Row>
    </Layout>
  );
};
