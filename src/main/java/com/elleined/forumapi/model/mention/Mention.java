package com.elleined.forumapi.model.mention;

import com.elleined.forumapi.model.NotificationStatus;
import com.elleined.forumapi.model.User;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Table(name = "tbl_mention")
@Inheritance(strategy = InheritanceType.TABLE_PER_CLASS)
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public abstract class Mention {

    @Id
    @GeneratedValue(
            strategy = GenerationType.TABLE,
            generator = "autoIncrement"
    )
    @SequenceGenerator(
            allocationSize = 1,
            name = "autoIncrement",
            sequenceName = "autoIncrement"
    )
    @Column(name = "mention_id")
    private int id;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @ManyToOne
    @JoinColumn(
            name = "mentioned_user",
            referencedColumnName = "user_id"
    )
    private User mentionedUser;

    @Column(name = "notification_status")
    @Enumerated(EnumType.STRING)
    private NotificationStatus notificationStatus;

    @ManyToOne
    @JoinColumn(
            name = "mentioning_user",
            referencedColumnName = "user_id"
    )
    private User mentioningUser;

    public abstract String getMessage();

    public abstract int getReceiverId();

    public abstract boolean isEntityActive();

    public boolean isRead() {
        return this.getNotificationStatus() == NotificationStatus.READ;
    }

    public boolean isUnread() {
        return this.getNotificationStatus() == NotificationStatus.UNREAD;
    }
}
