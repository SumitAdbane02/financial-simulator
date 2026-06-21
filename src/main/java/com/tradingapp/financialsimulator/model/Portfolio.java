package com.tradingapp.financialsimulator.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

/**
 * Represents a user's financial portfolio.
 * Each user has a single portfolio which holds their cash balance
 * and will later be linked to their stock assets.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "portfolios")
public class Portfolio {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // We use a One-to-One mapping to link a Portfolio directly to a User.
    // This is the "owning" side of the relationship.
    @OneToOne
    // @JoinColumn specifies the foreign key column in the 'portfolios' table.
    // 'name = "user_id"' is the name of the column that will be created.
    // 'referencedColumnName = "id"' specifies that 'user_id' will reference the 'id' column in the 'users' table.
    // 'nullable = false' ensures that a portfolio must always be associated with a user.
    // 'unique = true' reinforces the one-to-one nature of this relationship.
    @JoinColumn(name = "user_id", referencedColumnName = "id", nullable = false, unique = true)
    private User user;

    // Using BigDecimal for monetary values is a best practice to avoid floating-point inaccuracies.
    // 'precision' is the total number of digits.
    // 'scale' is the number of digits after the decimal point.
    // A precision of 19 and scale of 4 is suitable for large financial values (e.g., up to trillions with 4 decimal places).
    @Column(nullable = false, precision = 19, scale = 4)
    private BigDecimal cashBalance;

    // highlight-start
    // This defines the "one" side of the One-to-Many relationship between Portfolio and PortfolioAsset.
    // One portfolio can have many assets.
    //
    // mappedBy = "portfolio": This is CRITICAL. It tells JPA that the relationship is already
    // managed by the 'portfolio' field in the 'PortfolioAsset' class. This makes PortfolioAsset
    // the owning side and prevents JPA from creating a redundant join table.
    //
    // cascade = CascadeType.ALL: This propagates all persistence operations (save, update, delete)
    // from a Portfolio to its associated PortfolioAssets.
    //
    // orphanRemoval = true: This is a powerful and important feature. If a PortfolioAsset is removed
    // from this list (e.g., portfolio.getAssets().remove(someAsset)), the entity itself will be
    // deleted from the database. This is essential for managing the lifecycle of child entities.

    @OneToMany(mappedBy = "portfolio",cascade = CascadeType.ALL,orphanRemoval = true)
    private List<PortfolioAsset> assets=new ArrayList<>();
}