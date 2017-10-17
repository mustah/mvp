import * as React from 'react';
import {translate} from '../../../../services/translationService';
import {Category} from '../../../collection/models/Collections';
import {Column} from '../layouts/column/Column';
import {Layout} from '../layouts/layout/Layout';
import {Row} from '../layouts/row/Row';
import {Bold} from '../texts/Texts';
import './ProblemOverview.scss';

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
          <table className="Table" cellPadding={0} cellSpacing={0}>
            <thead>
            <tr>
              <th>{translate('grouping')}</th>
              <th>{translate('count')}</th>
            </tr>
            </thead>
            <tbody>
            <tr>
              <td>{translate('errands')}</td>
              <td>{translate('{{count}} errand', {count: unhandled.total})}</td>
            </tr>
            <tr>
              <td>{translate('residential areas')}</td>
              <td>{translate('{{count}} residential area', {count: unhandled.area.count})}</td>
            </tr>
            <tr>
              <td>{translate('product model')}</td>
              <td>{translate('{{count}} product', {count: unhandled.product_model.count})}</td>
            </tr>
            </tbody>
          </table>
        </Column>
        <Column className="ProblemOverview-grouping">
          <Bold className="ProblemOverview-title">{translate('action pending')}</Bold>
          <table className="Table" cellPadding={0} cellSpacing={0}>
            <thead>
            <tr>
              <th>{translate('grouping')}</th>
              <th>{translate('count')}</th>
            </tr>
            </thead>
            <tbody>
            <tr>
              <td>{translate('errands')}</td>
              <td>{translate('{{count}} errand', {count: handled.total})}</td>
            </tr>
            <tr>
              <td>{translate('residential areas')}</td>
              <td>{translate('{{count}} residential area', {count: handled.area.count})}</td>
            </tr>
            <tr>
              <td>{translate('product model')}</td>
              <td>{translate('{{count}} product', {count: handled.product_model.count})}</td>
            </tr>
            </tbody>
          </table>

        </Column>
      </Row>
    </Layout>
  );
};
