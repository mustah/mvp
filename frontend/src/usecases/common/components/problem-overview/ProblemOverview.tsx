import * as React from 'react';
import {translate} from '../../../../services/translationService';
import {IdNamed} from '../../../../types/Types';
import {Category} from '../../../collection/models/Collections';
import {DropdownSelector} from '../dropdown-selector/DropdownSelector';
import {SelectionParameter} from '../../../../state/search/selection/selectionModels';
import {SelectionState} from '../../../../state/search/selection/selectionReducer';
import {getDeselectedCities, getSelectedCities} from '../../../../state/search/selection/selectionSelectors';
import {Column} from '../layouts/column/Column';
import {Layout} from '../layouts/layout/Layout';
import {Row} from '../layouts/row/Row';
import {Bold} from '../texts/Texts';
import './ProblemOverview.scss';

interface ProblemOverviewProps {
  categories: Category;
  selection: SelectionState;
  toggleSearchOption: (searchParameters: SelectionParameter) => void;
}

export const ProblemOverview = (props: ProblemOverviewProps) => {
  const {categories: {handled, unhandled}, selection, toggleSearchOption} = props;
  const selectCity = (selection: IdNamed) => toggleSearchOption({...selection, entity: 'cities'});

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
              <td>{translate('cities')}</td>
              <td>
                <DropdownSelector
                  selectedList={getSelectedCities(selection)}
                  list={getDeselectedCities(selection)}
                  selectionText={translate('{{count}} city', {count: unhandled.city.count})}
                  onClick={selectCity}
                />
              </td>
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
              <td>{translate('cities')}</td>
              <td>{translate('{{count}} city', {count: handled.city.count})}</td>
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
