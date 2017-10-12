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

interface ProblemOverviewProps {
  categories: any;
}

export const ProblemOverview = (props: ProblemOverviewProps) => {
  const {categories: {handled, unhandled}} = props;

  // TODO the mapping between back and front end needs to be more formal

  const unhandledById = {};
  const allUnhandledIds: any = [];
  if (unhandled) {
    Object.keys(unhandled).map((c, i) => {
      allUnhandledIds.push(i);
      return unhandledById[i] = unhandled[i];
    });
  }
  const normalizedUnhandled = {
    allIds: allUnhandledIds,
    byId: unhandledById,
  };

  const handledById = {};
  const allHandledids: any = [];
  if (handled) {
    Object.keys(handled).map((c, i) => {
      allHandledids.push(i);
      return handledById[i] = handled[i];
    });
  }
  const normalizedHandled = {
    allIds: allHandledids,
    byId: handledById,
  };
  return (
    <Layout>
      <Row className="ProblemOverview">
        <Column className="ProblemOverview-grouping">
          <Bold className="ProblemOverview-title">{translate('unhandled problems')}</Bold>
          <Table data={normalizedUnhandled}>
            <TableColumn header={<TableHead>{translate('grouping')}</TableHead>} id="category"/>
            <TableColumn header={<TableHead>{translate('count')}</TableHead>} id="count"/>
          </Table>
        </Column>
        <Column className="ProblemOverview-grouping">
          <Bold className="ProblemOverview-title">{translate('action pending')}</Bold>
          <Table data={normalizedHandled}>
            <TableColumn header={<TableHead>{translate('grouping')}</TableHead>} id="category"/>
            <TableColumn header={<TableHead>{translate('count')}</TableHead>} id="count"/>
          </Table>
        </Column>
      </Row>
    </Layout>
  );
};
